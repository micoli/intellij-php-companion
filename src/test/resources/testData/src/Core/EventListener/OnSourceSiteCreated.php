<?php

declare(strict_types=1);

namespace App\Core\EventListener;

use App\Core\Article\Application\Event\SourceSiteCreatedEvent;
use App\Core\Article\Domain\Entity\Feed;
use App\Core\Article\Domain\Enum\FeedTypeEnum;
use App\Core\Article\Domain\Repository\FeedRepositoryInterface;
use App\Core\Article\Domain\Repository\SourceSiteRepositoryInterface;
use App\Core\Event\FeedCreatedEvent;
use App\Infrastructure\Bus\EventDispatcherInterface;
use App\Infrastructure\Bus\Handler\EventHandlerInterface;
use App\Infrastructure\Feed\DiscoveryInterface;
use Psr\Log\LoggerInterface;
use Webmozart\Assert\Assert;

final readonly class OnSourceSiteCreated implements EventHandlerInterface
{
    public function __construct(
        private LoggerInterface $logger,
        private DiscoveryInterface $discovery,
        private SourceSiteRepositoryInterface $sourceSiteRepository,
        private FeedRepositoryInterface $feedRepository,
        private EventDispatcherInterface $eventDispatcher,
    ) {
    }

    public function __invoke(SourceSiteCreatedEvent $siteCreatedEvent): void
    {
        $sourceSite = $this->sourceSiteRepository->getById($siteCreatedEvent->sourceSiteId);
        Assert::notNull($sourceSite, sprintf('Source site not found %s ', $siteCreatedEvent->sourceSiteId->toRfc4122()));
        $discoveredFeed = $this->discovery->discover($sourceSite->getUrl());

        if ($discoveredFeed === null) {
            $this->logger->error(sprintf('No feed discovered on %s ', $sourceSite->getUrl()), ['url' => $sourceSite->getUrl()]);

            return;
        }

        if ($this->feedRepository->existsByUrl($discoveredFeed->uri)) {
            $this->logger->error(sprintf('Feed already exists with url %s ', $discoveredFeed->uri), ['url' => $discoveredFeed->uri]);
        }

        $feed = new Feed($sourceSite, $discoveredFeed->uri, FeedTypeEnum::from($discoveredFeed->type->value), $discoveredFeed->imageUri);
        $this->feedRepository->add($feed);
        $this->eventDispatcher->dispatch(new FeedCreatedEvent($feed->getId()));
    }
}
