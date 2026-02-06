<?php

namespace App\Controller;

use App\Infrastructure\Bus\DomainEventDispatcher as DomainEventDispatcherInterface;
use App\UseCase\ArticleViewed;

class ControllerWithPartialThrows
{
    private DomainEventDispatcherInterface $eventDispatcher;

    /**
     * @throws \App\Exception\ArticleNotFoundException
     */
    public function showAction(int $id): void
    {
        $this->eventDispatcher->dispatch(new ArticleViewed\Event($id));
    }
}
