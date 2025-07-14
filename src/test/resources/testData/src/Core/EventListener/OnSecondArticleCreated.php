<?php

declare(strict_types=1);

namespace App\Core\EventListener;

use App\Core\Event\ArticleCreatedEvent;
use App\Core\Event\FeedCreatedEvent;
use App\Infrastructure\Bus\EventDispatcherInterface;
use App\Infrastructure\Bus\Handler\EventHandlerInterface;
use Psr\Log\LoggerInterface;

final readonly class OnSecondArticleCreated implements EventHandlerInterface
{
    public function __construct(
        private LoggerInterface $logger,
        private EventDispatcherInterface $eventDispatcher,
    ) {
    }

    public function __invoke(ArticleCreatedEvent $articleCreatedEvent): void
    {
        $this->eventDispatcher->dispatch(new FeedCreatedEvent($feed->getId()));
        $this->logger->warning(sprintf('Article created %s ', $articleCreatedEvent->articleId->toRfc4122()), ['id' => $articleCreatedEvent->articleId->toRfc4122()]);
    }
}
