<?php

namespace App\UseCase\ListArticles;

use App\Infrastructure\Bus\DomainEventDispatcher;
use App\Infrastructure\Bus\Handler\DomainQueryHandler;
use App\Repository\PostRepository;
use App\UseCase\ArticleViewed;
use App\UseCase\ArticleListed;

readonly class Handler implements DomainQueryHandler
{
    public function __construct(
        private PostRepository $postRepository,
        private DomainEventDispatcher $eventDispatcher,
    )
    {
    }

    public function __invoke(Query $query):Result
    {
        $latest = $this->postRepository->findLatest($query->page, $query->tag);

        foreach($latest->getResults() as $article){
            $id = $article->getId();
            assert(is_int($id));
            $this->eventDispatcher->dispatch(new ArticleListed\Event($id));
        }
        return new Result($latest);
    }
}
