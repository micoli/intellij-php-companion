<?php

declare(strict_types=1);

namespace App\Core\UseCase\UpdateFeed;

use App\Core\Article\Application\Service\FeedUpdater;
use App\Core\Article\Domain\Repository\FeedRepositoryInterface;
use App\Infrastructure\Assert\Validate;
use App\Infrastructure\Bus\Handler\CommandHandlerInterface;
use App\Shared\Exception\NotFoundException;

final readonly class Handler implements CommandHandlerInterface
{
    public function __construct(
        private FeedUpdater $feedUpdater,
        private FeedRepositoryInterface $feedRepository,
    ) {
    }

    public function __invoke(Command $command): void
    {
        $feed = $this->feedRepository->getById($command->feedId);
        Validate::notNull($feed, sprintf('Feed not found %s ', $command->feedId->toRfc4122()), NotFoundException::class);

        $this->feedUpdater->update($feed);
    }
}
