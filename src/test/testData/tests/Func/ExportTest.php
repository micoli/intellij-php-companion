<?php

declare(strict_types=1);

namespace App\Tests;

use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;
use Symfony\Component\VarDumper\Cloner\VarCloner;
use Symfony\Component\VarDumper\Dumper\CliDumper;


final class ExportTest extends TestCase
{
    public static function cliDataProvider(): iterable
    {
        $class = new class {
            public $name = 'Anonymous1';
            public $subClass = null;
        };
        $subClass = new class {
            public $name = 'Anonymous2';
        };
        $class->subClass = $subClass;
        yield [null];
        yield ["abc"];
        yield [1];
        yield [["a_class"=>$class]];
        yield [["a_class"=>new TestDTO("known")]];
        yield [[
            "quiz" => [
                "sports" => [
                    new TestDTO("known1"),
                    new TestDTO("known2"),
                    $class
                ]
            ]
        ]];
        yield [[
            "quiz" => [
                "sport" => [
                    "q1" => [
                        "question" => "Which one is correct team name in NBA?",
                        "options" => [
                            [
                                "city" => "New York Bulls"
                            ],
                            [
                                "city" => "Los Angeles Kings"
                            ],
                            [
                                "city" => "Golden State Warriros"
                            ],
                            [
                                "city" => "Huston Rocket"
                            ]
                        ],
                        "answer" => "Huston Rocket"
                    ]
                ],
                "maths" => [
                    "q1" => [
                        "question" => "5 + 7 = ?",
                        "options" => [
                            "10",
                            "11",
                            "12",
                            "13"
                        ],
                        "answer" => "12"
                    ],
                    "q2" => [
                        "question" => "12 - 8 = ?",
                        "options" => [
                            "1",
                            "2",
                            "3",
                            "4"
                        ],
                        "answer" => "4"
                    ]
                ]
            ]
        ]
        ];
    }

    #[DataProvider('cliDataProvider')]
    public function testJson(mixed $data): void
    {
        dump($data);
        $dumpString = $this->captureCliDumperWithStream($data);
        echo "------<\n";
        echo $dumpString;
        echo "------\n";
        echo $this->parseCliDumperToJson($dumpString);
        echo "------>\n";
        self::assertSame("a", "a");
    }

    private function captureCliDumperWithStream($data)
    {
        $cloner = new VarCloner();

        $stream = fopen('php://memory', 'r+');
        $dumper = new CliDumper($stream);
        $dumper->dump($cloner->cloneVar($data));
        rewind($stream);
        $output = stream_get_contents($stream);
        fclose($stream);

        return $output;
    }

    private function parseCliDumperToJson($cliDumperOutput)
    {
        $output = trim($cliDumperOutput);
        $output = str_replace('\\n', '', $output);
        $result = $this->parseValue($output);

        return json_encode($result, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
    }

    function parseValue($input)
    {
        $input = trim($input);

        if (preg_match('/^array:\d+\s*\[\s*(.*)?\s*\]$/s', $input, $matches)) {
            return $this->parseArray($matches[1] ?? '');
        }

        if (preg_match('/^"(.*)"$/', $input, $matches)) {
            return $matches[1];
        }

        if (is_numeric($input)) {
            return is_float($input + 0) ? (float)$input : (int)$input;
        }

        if ($input === 'true') return true;
        if ($input === 'false') return false;
        if ($input === 'null') return null;

        return $input;
    }

    function parseArray($content)
    {
        if (empty(trim($content))) {
            return [];
        }

        $result = [];
        $items = $this->splitArrayItems($content);

        foreach ($items as $item) {
            $item = trim($item);

            if (preg_match('/^"([^"]*)"\\s*=>\\s*(.+)$/s', $item, $matches)) {
                $key = $matches[1];
                $value = trim($matches[2]);
                $result[$key] = $this->parseValue($value);
            } else if (preg_match('/^([^\\s=>]+)\\s*=>\\s*(.+)$/s', $item, $matches)) {
                $key = $matches[1];
                $value = trim($matches[2]);
                $result[$key] = $this->parseValue($value);
            } else if (preg_match('/^(\\d+)\\s*=>\\s*(.+)$/s', $item, $matches)) {
                $index = (int)$matches[1];
                $value = trim($matches[2]);
                $result[$index] = $this->parseValue($value);
            } else {
                $result[] = $this->parseValue($item);
            }
        }

        return $result;
    }

    function splitArrayItems($content)
    {
        $items = [];
        $current = '';
        $depth = 0;
        $inString = false;
        $escapeNext = false;

        for ($i = 0; $i < strlen($content); $i++) {
            $char = $content[$i];

            if ($escapeNext) {
                $current .= $char;
                $escapeNext = false;
                continue;
            }

            if ($char === '\\') {
                $escapeNext = true;
                $current .= $char;
                continue;
            }

            if ($char === '"') {
                $inString = !$inString;
                $current .= $char;
                continue;
            }

            if (!$inString) {
                if ($char === '[') {
                    $depth++;
                } else if ($char === ']') {
                    $depth--;
                }

                if ($char === "\n" && $depth === 0) {
                    $trimmed = trim($current);
                    if (!empty($trimmed)) {
                        $items[] = $trimmed;
                    }
                    $current = '';
                    continue;
                }
            }

            $current .= $char;
        }

        $trimmed = trim($current);
        if (!empty($trimmed)) {
            $items[] = $trimmed;
        }

        return $items;
    }
}

final readonly class TestDTO
{
    public function __construct(public string $name)
    {
    }
}
