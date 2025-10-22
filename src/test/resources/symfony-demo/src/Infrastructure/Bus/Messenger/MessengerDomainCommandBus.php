<?php

declare(strict_types=1);

namespace App\Infrastructure\Bus\Messenger;

use App\Infrastructure\Bus\DomainCommandBus;
use App\Infrastructure\Bus\Message\Command\DomainCommand;
use App\Infrastructure\Bus\Message\Command\SyncDomainCommandResult;
use App\Infrastructure\Bus\Message\Command\SyncDomainCommandWithResult;
use App\Infrastructure\Bus\TraceableBusInterface;
use LogicException;
use Symfony\Component\Messenger\MessageBusInterface;
use Symfony\Component\Messenger\Stamp\HandledStamp;
use Symfony\Component\Messenger\TraceableMessageBus;
use Throwable;

final readonly class MessengerDomainCommandBus implements DomainCommandBus, TraceableBusInterface
{
    /** @param MessageBusInterface&TraceableMessageBus $bus */
    public function __construct(
        private MessageBusInterface $bus,
    ) {
    }

    public function dispatch(DomainCommand $command): void
    {
        $this->bus->dispatch($command);
    }

    /**
     * @psalm-suppress InvalidReturnStatement
     * @psalm-suppress MixedReturnStatement
     * @psalm-suppress MixedInferredReturnType
     */
    public function syncDispatch(SyncDomainCommandWithResult $command): SyncDomainCommandResult
    {
        try {
            /** @var ?HandledStamp $handledStamp */
            $handledStamp = $this->bus->dispatch($command)->last(HandledStamp::class);
            if ($handledStamp === null) {
                throw new LogicException('Sync command did not sent a result');
            }

            /** @phpstan-ignore-next-line  */
            return $handledStamp->getResult();
        } catch (Throwable $e) {
            dd($e);
        }
    }

    public function getDispatchedMessages(): array
    {
        return $this->bus->getDispatchedMessages();
    }
}
