<?php

declare(strict_types=1);

namespace App\Infrastructure\Bus;

use App\Infrastructure\Bus\Message\Command\CommandInterface;
use App\Infrastructure\Bus\Message\Command\SyncCommandResultInterface;
use App\Infrastructure\Bus\Message\Command\SyncCommandWithResultInterface;

interface CommandBusInterface
{
    public function dispatch(CommandInterface $command): void;

    /**
     * @template T of SyncCommandResultInterface
     *
     * @param SyncCommandWithResultInterface<T> $command
     *
     * @return T
     */
    public function syncDispatch(SyncCommandWithResultInterface $command): SyncCommandResultInterface;
}
