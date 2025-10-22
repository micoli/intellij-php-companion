<?php

namespace App\UseCase\ListArticles;

use App\Infrastructure\Bus\Message\Query\SyncDomainQueryResult;
use App\Pagination\Paginator;

class Result implements SyncDomainQueryResult
{

    public function __construct(public Paginator $findLatest)
    {
    }
}
