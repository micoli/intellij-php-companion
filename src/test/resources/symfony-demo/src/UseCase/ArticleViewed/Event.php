<?php

namespace App\UseCase\ArticleViewed;

use App\Infrastructure\Bus\Message\Event\SyncDomainEvent;

readonly class Event implements SyncDomainEvent
{
public  function __construct(
    public int $id
)
{
}
}
