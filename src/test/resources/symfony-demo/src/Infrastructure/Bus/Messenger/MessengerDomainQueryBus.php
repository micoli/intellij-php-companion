<?php

declare(strict_types=1);

namespace App\Infrastructure\Bus\Messenger;

use App\Infrastructure\Bus\Message\Query\SyncDomainQuery;
use App\Infrastructure\Bus\Message\Query\SyncDomainQueryResult;
use App\Infrastructure\Bus\DomainQueryBusInterface;
use App\Infrastructure\Bus\TraceableBusInterface;
use Symfony\Component\Messenger\MessageBusInterface;
use Symfony\Component\Messenger\Stamp\HandledStamp;
use Symfony\Component\Messenger\TraceableMessageBus;
use Throwable;

final readonly class MessengerDomainQueryBus implements DomainQueryBusInterface, TraceableBusInterface
{
    /** @param MessageBusInterface&TraceableMessageBus $queryBus */
    public function __construct(
        private MessageBusInterface $queryBus,
    ) {
    }

    /**
     * @psalm-suppress InvalidReturnStatement
     * @psalm-suppress MixedReturnStatement
     * @psalm-suppress MixedInferredReturnType
     */
    public function query(SyncDomainQuery $query): SyncDomainQueryResult
    {
        try {
            /** @phpstan-ignore-next-line  */
            return $this->queryBus->dispatch($query)->last(HandledStamp::class)?->getResult();
        } catch (Throwable $e) {
            throw $e;
        }
    }

    public function getDispatchedMessages(): array
    {
        return $this->queryBus->getDispatchedMessages();
    }
}
