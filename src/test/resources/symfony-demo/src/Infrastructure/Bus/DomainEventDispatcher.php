<?php

declare(strict_types=1);

namespace App\Infrastructure\Bus;

use App\Infrastructure\Bus\Message\Event\DomainEvent;

interface DomainEventDispatcher
{
    public function dispatch(DomainEvent $event): void;
}
