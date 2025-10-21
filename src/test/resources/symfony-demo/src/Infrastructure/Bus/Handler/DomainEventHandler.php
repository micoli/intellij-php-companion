<?php

declare(strict_types=1);

namespace App\Infrastructure\Bus\Handler;

use Symfony\Component\DependencyInjection\Attribute\AutoconfigureTag;

#[AutoconfigureTag('messenger.message_handler', ['bus' => 'event.bus'])]
interface DomainEventHandler
{
}
