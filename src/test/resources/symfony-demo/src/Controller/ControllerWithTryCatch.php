<?php

namespace App\Controller;

use App\Infrastructure\Bus\DomainEventDispatcher as DomainEventDispatcherInterface;
use App\UseCase\ArticleViewed;
use App\Exception\ArticleNotFoundException;

class ControllerWithTryCatch
{
    private DomainEventDispatcherInterface $eventDispatcher;

    public function showAction(int $id): void
    {
        try {
            $this->eventDispatcher->dispatch(new ArticleViewed\Event($id));
        } catch (ArticleNotFoundException $e) {
            // ArticleNotFoundException is caught, only PermissionDeniedException should be missing
        }
    }
}
