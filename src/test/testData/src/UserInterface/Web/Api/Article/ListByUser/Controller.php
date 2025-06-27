<?php

declare(strict_types=1);

namespace App\UserInterface\Web\Api\Article\ListByUser;

use App\Core\Article\Application\Query\Articles;
use App\Core\Member\Application\Query\UserFeeds;
use App\Core\Member\Application\Query\UserTags;
use App\Core\Member\Domain\Entity\User;
use App\Infrastructure\Bus\QueryBusInterface;
use App\UserInterface\Web\Api\Article\ArticleEnricher;
use App\UserInterface\Web\Api\Article\ArticlesResponse;
use App\UserInterface\Web\Api\Article\Models\Article;
use FOS\RestBundle\Controller\Annotations\QueryParam;
use Nelmio\ApiDocBundle\Annotation\Model;
use OpenApi\Attributes as OA;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpKernel\Attribute\AsController;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Security\Http\Attribute\CurrentUser;

#[AsController]
final readonly class Controller
{
    public function __construct(
        private QueryBusInterface $queryBus,
        private ArticleEnricher $articleEnricher,
    ) {
    }

    #[Route(
        '/api/articles/user',
        name: 'api_articles_by_user',
        methods: ['GET'],
    )]
    #[QueryParam(
        name: 'page',
        requirements: '\d+',
        default: '1',
    )]
    #[OA\Response(
        response: 200,
        description: '',
        content: new OA\JsonContent(ref: new Model(type: ArticlesResponse::class)),
    )]
    public function list(
        #[CurrentUser] User $user,
        int $page,
    ): JsonResponse {
        $tags = $this->queryBus->query(new UserTags\Query($user->getId()))->articleTags;
        $feedIds = $this->queryBus->query(new UserFeeds\Query($user->getId()))->feedIds;

        return new JsonResponse(
            new ArticlesResponse(
                $page,
                array_map(
                    Article::from(...),
                    $this->articleEnricher->enrich(
                        $user,
                        $this->queryBus->query(new Articles\Query(
                            page: $page,
                            tags: $tags,
                            feedIds: $feedIds,
                        ))->articles,
                    ),
                ),
            ),
        );
    }
}
