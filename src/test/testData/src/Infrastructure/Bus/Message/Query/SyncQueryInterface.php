<?php

declare(strict_types=1);

namespace App\Infrastructure\Bus\Message\Query;

/**
 * @template T of ?SyncQueryResultInterface
 */
interface SyncQueryInterface extends QueryInterface
{
}
