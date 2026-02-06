<?php

namespace App\Controller;

use App\Infrastructure\Bus\DomainEventDispatcher as DomainEventDispatcherInterface;
use App\UseCase\ArticleViewed;
use App\Exception\ArticleNotFoundException;

class ControllerWithAllThrows
{
    private DomainEventDispatcherInterface $eventDispatcher;

    /**
     * @throws ArticleNotFoundException
     * @throws \App\Exception\PermissionDeniedException
     */
    public function showAction(int $id): void
    {
        $this->eventDispatcher->dispatch(new ArticleViewed\Event($id));
    }
}
