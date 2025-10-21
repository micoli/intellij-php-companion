<?php

declare(strict_types=1);

namespace App\Infrastructure\Bus\Message\Command;

/**
 * @template T of ?SyncDomainCommandResult
 */
interface SyncDomainCommandWithResult extends DomainCommand
{
}
