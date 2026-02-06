<?php

namespace App\Controller;

use App\Infrastructure\Bus\DomainEventDispatcher as DomainEventDispatcherInterface;
use App\UseCase\ArticleViewed;
use App\Exception\ArticleNotFoundException;
use App\Exception\PermissionDeniedException;

class ControllerWithTryCatchAll
{
    private DomainEventDispatcherInterface $eventDispatcher;

    public function showAction(int $id): void
    {
        try {
            $this->eventDispatcher->dispatch(new ArticleViewed\Event($id));
        } catch (ArticleNotFoundException | PermissionDeniedException $e) {
            // Both exceptions are caught, no warning needed
        }
    }
}
