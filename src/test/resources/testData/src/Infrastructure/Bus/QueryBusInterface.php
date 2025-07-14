<?php

declare(strict_types=1);

namespace App\Infrastructure\Bus;

use App\Infrastructure\Bus\Message\Query\SyncQueryInterface;
use App\Infrastructure\Bus\Message\Query\SyncQueryResultInterface;

interface QueryBusInterface
{
    /**
     * @template T of SyncQueryResultInterface
     *
     * @param SyncQueryInterface<T> $query
     *
     * @return T
     */
    public function query(SyncQueryInterface $query): SyncQueryResultInterface;
}
