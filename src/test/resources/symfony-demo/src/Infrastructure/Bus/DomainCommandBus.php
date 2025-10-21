<?php

declare(strict_types=1);

namespace App\Infrastructure\Bus;

use App\Infrastructure\Bus\Message\Command\DomainCommand;
use App\Infrastructure\Bus\Message\Command\SyncDomainCommandResult;
use App\Infrastructure\Bus\Message\Command\SyncDomainCommandWithResult;

interface DomainCommandBus
{
    public function dispatch(DomainCommand $command): void;

    /**
     * @template T of SyncDomainCommandResult
     *
     * @param SyncDomainCommandWithResult<T> $command
     *
     * @return T
     */
    public function syncDispatch(SyncDomainCommandWithResult $command): SyncDomainCommandResult;
}
