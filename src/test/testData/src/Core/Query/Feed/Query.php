<?php

declare(strict_types=1);

namespace App\Core\Query\Feed;

use App\Core\Id\FeedId;
use App\Infrastructure\Bus\Message\Query\SyncQueryInterface;

/**
 * @template-implements SyncQueryInterface<Result>
 */
final readonly class Query implements SyncQueryInterface
{
    /** @param list<FeedId> $feedIds */
    public function __construct(
        public array $feedIds,
    ) {
    }
}
