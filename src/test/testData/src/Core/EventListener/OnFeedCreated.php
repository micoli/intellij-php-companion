<?php

declare(strict_types=1);

namespace App\Core\EventListener;

use App\Core\Article\Application\Service\FeedUpdater;
use App\Core\Article\Domain\Repository\FeedRepositoryInterface;
use App\Core\Event\FeedCreatedEvent;
use App\Infrastructure\Bus\Handler\EventHandlerInterface;
use Webmozart\Assert\Assert;

final readonly class OnFeedCreated implements EventHandlerInterface
{
    public function __construct(
        private FeedUpdater $feedUpdater,
        private FeedRepositoryInterface $feedRepository,
    ) {
    }

    public function __invoke(FeedCreatedEvent $siteCreatedEvent): void
    {
        $feed = $this->feedRepository->getById($siteCreatedEvent->feedId);
        Assert::notNull($feed, sprintf('Feed not found %s ', $siteCreatedEvent->feedId->toRfc4122()));
        $this->feedUpdater->update($feed);
    }
}
