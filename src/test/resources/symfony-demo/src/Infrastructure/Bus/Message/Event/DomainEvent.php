<?php

declare(strict_types=1);

namespace App\Infrastructure\Bus\Message\Event;

use App\Infrastructure\Bus\Message\DomainMessage;

interface DomainEvent extends DomainMessage
{
}
