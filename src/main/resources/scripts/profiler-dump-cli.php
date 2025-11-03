<?php
declare(strict_types=1);

$projectPath = $argv[1] ?? '../../../test/resources/symfony-demo/';
$profilerStoragePath = $argv[2] ?? '../../../test/resources/symfony-demo/var/cache/dev/profiler';
$token = $argv[3] ?? 'bc8b3a';//c3f426
$page = $argv[4] ?? 'request';

require "{$projectPath}vendor/autoload.php";


use Symfony\Component\HttpKernel\Profiler\FileProfilerStorage;
use Symfony\Component\HttpKernel\Profiler\Profile;
use Symfony\Component\VarDumper\Cloner\Data;

interface ProfilerExporter
{
    public function getKey(): string;

    public function export(Profile $profile): array;
}

class DbExporter implements ProfilerExporter
{
    public function getKey(): string
    {
        return 'db';
    }

    public function export(Profile $profile): array
    {
        /** @var Doctrine\Bundle\DoctrineBundle\DataCollector\DoctrineDataCollector $collector */
        $collector = $profile->getCollector("db");
        $entities = [];
        foreach ($collector->getEntities() as $connection => $doctrineEntities) {
            foreach ($doctrineEntities as $entity) {
                $entities[] = $entity['class'];
            }
        }

        $queries = [];
        $queryIndex = 0;
        $twigExtension = new Doctrine\Bundle\DoctrineBundle\Twig\DoctrineExtension();
        $statements = [];
        foreach ($collector->getQueries() as $connection => $doctrineQueries) {
            foreach ($doctrineQueries as $query) {
                $hash = md5($query['sql']);
                if (!isset($statements[$hash])) {
                    $statements[$hash] = 0;
                }
                $statements[$hash]++;
                $queries[] = [
                    'index' => $queryIndex++,
                    'connection' => $connection,
                    'sql' => $query['sql'],
                    'runnableSql' => $twigExtension->replaceQueryParameters($query['sql'], $query['params']),
                    'htmlSql' => $twigExtension->formatSql($query['sql'], true),
                    'backtrace' => array_map(static fn(array $backtrace) => [
                        "file" => $backtrace["file"],
                        "line" => $backtrace["line"],
                    ], $query['backtrace']),
                ];
            }
        }
        return [
            'queries' => $queries,
            'entities' => $entities,
            'stats' => [
                "databaseQueriesCount" => $collector->getQueryCount(),
                "differentStatmentsCount" => count($statements),
                "queryTime" => $collector->getTime(),
            ],
        ];
    }

}

class RequestExporter implements ProfilerExporter
{
    public function getKey(): string
    {
        return 'request';
    }

    public function export(Profile $profile): array
    {
        /** @var Symfony\Component\HttpKernel\DataCollector\RequestDataCollector $collector */
        $collector = $profile->getCollector("request");
        $controllerData = $collector->getController();
        $controller = is_string($controllerData) ? $controllerData : $controllerData->getValue(true)['class'];
        return [
            'route' => $collector->getRoute(),
            'controller' => $controller,/*[
                'class' => $controller['class'],
                'file' => $controller['file'],
                'line' => $controller['line'],
                'method' => $controller['method'],
            ]*/
            // 'identifier' => $collector->getIdentifier(),
        ];
    }

}

class LoggerExporter implements ProfilerExporter
{
    public function getKey(): string
    {
        return 'logger';
    }

    public function export(Profile $profile): array
    {
        /** @var Symfony\Component\HttpKernel\DataCollector\LoggerDataCollector $collector */
        $collector = $profile->getCollector("logger");
        return [
            'logs' => array_map(static fn(array $log) => [
                'time' => $log['timestamp_rfc3339'],
                'channel' => $log['channel'],
                'severity' => $log['priorityName'],
                'message' => $log['message'],
                'context' => json_encode($log['context']),
            ], $collector->getLogs()->getValue(true)),
        ];
    }

}

class MessengerExporter implements ProfilerExporter
{
    public function getKey(): string
    {
        return 'messenger';
    }

    public function export(Profile $profile): array
    {
        /** @var Symfony\Component\Messenger\DataCollector\MessengerDataCollector $collector */
        $collector = $profile->getCollector("messenger");
        return [
            'stats' => [
                "messageCount" => count($collector->getMessages()),
            ],
            'dispatches' => array_map(function (Data $message) {
                return [
                    'messageName' => (string)$message['message']['type'],
                    'busName' => $message['bus'],
                    'message' => json_encode($message['message']['value']->getValue(true)),
                    'dispatch' => [
                        'file' => $message['caller']['file'],
                        'line' => $message['caller']['line'],
                    ]
                ];
            }, $collector->getMessages()),
        ];
    }

}

class ProfilerDumpExporter
{
    private FileProfilerStorage $profilerStorage;

    /** @var ProfilerExporter[] */
    private array $formatters = [
        DbExporter::class,
        RequestExporter::class,
        LoggerExporter::class,
        MessengerExporter::class,
    ];

    public function __construct(string $profilerStoragePath)
    {
        $this->profilerStorage = new FileProfilerStorage("file:$profilerStoragePath");
    }

    public function export(string $page, string $token): array
    {
        $data = $this->profilerStorage->read($token);
        foreach ($this->formatters as $formatterClass) {
            $exporter = new $formatterClass();
            if ($exporter->getKey() === $page) {
                return $exporter->export($data);
            }
        }
        return [];
    }

}

echo json_encode(
    (new ProfilerDumpExporter($profilerStoragePath))->export($page, $token),
    JSON_PRETTY_PRINT,
);
