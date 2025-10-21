<?php

declare(strict_types=1);

namespace App\Infrastructure\Bus;

interface TraceableBusInterface
{
    /**
     * @return mixed[]
     */
    public function getDispatchedMessages(): array;
}
