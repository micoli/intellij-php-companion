<?php

declare(strict_types=1);

namespace App\Infrastructure\Bus\Messenger;

use App\Infrastructure\Bus\DomainEventDispatcher;
use App\Infrastructure\Bus\Message\Event\DomainEvent;
use App\Infrastructure\Bus\TraceableBusInterface;
use Symfony\Component\Messenger\MessageBusInterface;
use Symfony\Component\Messenger\TraceableMessageBus;

final readonly class MessengerDomainEventDispatcher implements DomainEventDispatcher, TraceableBusInterface
{
    /** @param MessageBusInterface&TraceableMessageBus $eventBus */
    public function __construct(
        private MessageBusInterface $eventBus,
    ) {
    }

    public function dispatch(DomainEvent $event): void
    {
        $this->eventBus->dispatch($event);
    }

    public function getDispatchedMessages(): array
    {
        return $this->eventBus->getDispatchedMessages();
    }
}
