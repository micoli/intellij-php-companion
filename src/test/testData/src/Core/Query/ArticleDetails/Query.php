<?php

declare(strict_types=1);

namespace App\Core\Query\ArticleDetails;

use App\Core\Id\ArticleId;
use App\Core\Models\Article;
use App\Infrastructure\Bus\Message\Query\SyncQueryInterface;

/**
 * @template-implements SyncQueryInterface<Result>
 */
final readonly class Query implements SyncQueryInterface
{
    /** @param list<ArticleId> $articleIds */
    public function __construct(
        public array $articleIds,
    ) {
    }
}
