<?php

declare(strict_types=1);

namespace App\Core\Query\Article;

use App\Core\Query\ArticleDTO;
use App\Infrastructure\Bus\Message\Query\SyncQueryResultInterface;

final readonly class Result implements SyncQueryResultInterface
{
    public function __construct(
        public ?ArticleDTO $article,
    ) {
    }
}
