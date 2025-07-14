<?php

declare(strict_types=1);

namespace App\Infrastructure\Bus;

use App\Infrastructure\Bus\Message\Event\EventInterface;

interface EventDispatcherInterface
{
    public function dispatch(EventInterface $event): void;
}
