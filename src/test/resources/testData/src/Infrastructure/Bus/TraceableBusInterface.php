<?php

declare(strict_types=1);

namespace App\Infrastructure\Bus;

interface TraceableBusInterface
{
    public function getDispatchedMessages(): array;
}
