<?php

declare(strict_types=1);

namespace App\Infrastructure\Bus\Message\Command;

/**
 * @template T of ?SyncCommandResultInterface
 */
interface SyncCommandWithResultInterface extends CommandInterface
{
}
