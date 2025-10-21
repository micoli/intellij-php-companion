<?php

namespace App\UseCase\ListArticles;

use App\Entity\Tag;
use App\Infrastructure\Bus\Message\Query\SyncDomainQuery;

/**
 * @template-implements SyncDomainQuery<Result>
 */
readonly class Query implements SyncDomainQuery
{
    public function __construct(
        public int  $page = 1,
        public ?Tag $tag = null
    )
    {
    }

    public static function create(int  $page = 1, Tag $tag = null): Query{
        return new self($page, $tag);
    }
}
