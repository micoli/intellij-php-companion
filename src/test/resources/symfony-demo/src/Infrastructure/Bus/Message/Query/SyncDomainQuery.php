<?php

declare(strict_types=1);

namespace App\Infrastructure\Bus\Message\Query;

/**
 * @template T of SyncDomainQueryResult|null
 */
interface SyncDomainQuery extends DomainQuery
{
}
