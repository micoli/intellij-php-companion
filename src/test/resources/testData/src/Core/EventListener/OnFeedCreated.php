<?php

declare(strict_types=1);

namespace App\Core\EventListener;

use App\Core\Article\Application\Service\FeedUpdater;
use App\Core\Article\Domain\Repository\FeedRepositoryInterface;
use App\Core\Event\FeedCreatedEvent;
use App\Core\Event\ArticleCreatedEvent;
use App\Infrastructure\Bus\EventDispatcherInterface;
use App\Infrastructure\Bus\Handler\EventHandlerInterface;
use Webmozart\Assert\Assert;

final readonly class OnFeedCreated implements EventHandlerInterface
{
    public function __construct(
        private FeedUpdater $feedUpdater,
        private FeedRepositoryInterface $feedRepository,
        private EventDispatcherInterface $eventDispatcher,
    ) {
    }

    public function __invoke(FeedCreatedEvent $siteCreatedEvent): void
    {
        $feed = $this->feedRepository->getById($siteCreatedEvent->feedId);
        $this->eventDispatcher->dispatch(new ArticleCreatedEvent($feed->getId()));
        Assert::notNull($feed, sprintf('Feed not found %s ', $siteCreatedEvent->feedId->toRfc4122()));
        $this->feedUpdater->update($feed);
    }
}
