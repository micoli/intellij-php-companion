<?php

declare(strict_types=1);

namespace App\Tests;

use App\Tests\Func\Models\AnInnerObject;
use App\Tests\Func\Models\AnObject;
use Cake\Chronos\Chronos;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;
use Ramsey\Uuid\Uuid;
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
        yield [["a_complex_class" => new AnObject(
            Uuid::uuid4(),
            "A name",
            null,
            Chronos::now(),
            new AnInnerObject(
                Uuid::uuid4(),
                "Another name",
                "Another label",
                Chronos::now(),
                Chronos::now(),
            ))]];
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
        echo "------<\n\n";
        echo $dumpString;
        echo "\n\n------>\n";
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
}

final readonly class TestDTO
{
    public function __construct(public string $name)
    {
    }
}
