<?php

namespace App\Controller;

use App\Infrastructure\Bus\DomainEventDispatcher as DomainEventDispatcherInterface;
use App\UseCase\ArticleViewed;

class ControllerWithMissingThrows
{
    private DomainEventDispatcherInterface $eventDispatcher;

    public function showAction(int $id): void
    {
        $this->eventDispatcher->dispatch(new ArticleViewed\Event($id));
    }
}
