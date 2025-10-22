<?php

declare(strict_types=1);

namespace App\Infrastructure\Bus;

use App\Infrastructure\Bus\Message\Query\SyncDomainQuery;
use App\Infrastructure\Bus\Message\Query\SyncDomainQueryResult;

interface DomainQueryBusInterface
{
    /**
     * @template T of SyncDomainQueryResult
     *
     * @param SyncDomainQuery<T> $query
     *
     * @return T
     */
    public function query(SyncDomainQuery $query): SyncDomainQueryResult;
}
