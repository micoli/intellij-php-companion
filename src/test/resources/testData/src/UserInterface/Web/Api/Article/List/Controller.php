<?php

declare(strict_types=1);

namespace App\UserInterface\Web\Api\Article\List;

use App\Core\Query\Article as Articles;
use App\Core\Query\ArticleDetails as ArticleDetails;
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
    private string $dispatch;

    public function __construct(
        private QueryBusInterface $queryBus,
        private ArticleEnricher   $articleEnricher,
    )
    {
    }

    #[Route(
        '/api/articles/list',
        name: 'api_articles_list',
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
    public function home(
        #[CurrentUser] ?User $user,
        int                  $page,
    ): JsonResponse
    {
        $query = new Articles\Query();
        $this->queryBus->query($query)->articles;
        $this->notify($query)->articles;
        $var = $this->handle();
        $this->dispatch = "123";
        $this->queryBus->query(new ArticleDetails\Query())->articles;

        return new JsonResponse(
            new ArticlesResponse(
                $page,
                array_map(
                    Article::from(...),
                    $this->articleEnricher->enrich(
                        $user,
                        $this->queryBus->query(new Articles\Query(
                            page: $page,
                        ))->articles,
                    ),
                ),
            ),
        );
    }

    public function notify(Articles\Query $query): mixed
    {
        return $this->queryBus->query($query);
    }

    public function handle(): mixed
    {
        return 123;
    }
}
