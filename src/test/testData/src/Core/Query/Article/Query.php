<?php

declare(strict_types=1);

namespace App\Core\Query\Article;

use App\Core\Id\ArticleId;
use App\Infrastructure\Bus\Message\Query\SyncQueryInterface;

/**
 * @template-implements SyncQueryInterface<Result>
 */
final readonly class Query implements SyncQueryInterface
{
    public function __construct(
        public ArticleId $articleId,
    ) {
    }
}
