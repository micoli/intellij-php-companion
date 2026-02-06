<?php

namespace App\UseCase\ArticleViewed;

use App\Infrastructure\Bus\Handler\DomainEventHandler;

class HandlerWithThrows implements DomainEventHandler
{
    /**
     * @throws \App\Exception\ArticleNotFoundException
     * @throws \App\Exception\PermissionDeniedException
     */
    public function __invoke(Event $event): void
    {
        // handler logic that may throw
    }
}
