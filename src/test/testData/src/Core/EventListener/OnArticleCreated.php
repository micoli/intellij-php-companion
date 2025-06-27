<?php

declare(strict_types=1);

namespace App\Core\EventListener;

use App\Core\Event\ArticleCreatedEvent;
use App\Infrastructure\Bus\Handler\EventHandlerInterface;
use Psr\Log\LoggerInterface;

final readonly class OnArticleCreated implements EventHandlerInterface
{
    public function __construct(
        private LoggerInterface $logger,
    ) {
    }

    public function __invoke(ArticleCreatedEvent $articleCreatedEvent): void
    {
        $this->logger->warning(sprintf('Article created %s ', $articleCreatedEvent->articleId->toRfc4122()), ['id' => $articleCreatedEvent->articleId->toRfc4122()]);
    }
}
