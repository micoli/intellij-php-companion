<?php

declare(strict_types=1);

namespace App\Infrastructure\Persistence;

abstract class AbstractUuid
{
    private static int $lastGeneratedId = 0;

    private function __construct(private int $id)
    {
    }

    public function getId(): int
    {
        return $this->id;
    }

    public static function create(): static
    {
        ++self::$lastGeneratedId;
        return new static(self::$lastGeneratedId);
    }
}
