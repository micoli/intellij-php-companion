<?php

declare(strict_types=1);

namespace App\Core\EventListener;

use App\Core\Article\Application\Event\TestCreatedEvent;
use App\Infrastructure\Bus\Handler\EventHandlerInterface;
use Psr\Log\LoggerInterface;

final readonly class OnTestCreated implements EventHandlerInterface
{
    public function __construct(
        private LoggerInterface $logger,
    ) {
    }

    public function __invoke(TestCreatedEvent $testCreatedEvent): void
    {
        $this->logger->warning(sprintf('Test created %s ', $testCreatedEvent->id), ['id' => $testCreatedEvent->id]);
    }
}
