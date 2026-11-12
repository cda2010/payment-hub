(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('ligne-frais', {
            parent: 'entity',
            url: '/ligne-frais?page&sort&search',
            data: {
                authorities: ['ROLE_CRE_FRAIS'],
                pageTitle: 'ptiApp.ligneFrais.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/components/tables/tables.html',
                    controller: 'LigneFraisController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('ligneFrais');
                    $translatePartialLoader.addPart('typeTarif');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('ligne-frais-detail', {
            parent: 'ligne-frais',
            url: '/ligne-frais/{id}',
            data: {
                authorities: ['ROLE_CRE_FRAIS'],
                pageTitle: 'ptiApp.ligneFrais.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/ligne-frais/ligne-frais-detail.html',
                    controller: 'LigneFraisDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('ligneFrais');
                    $translatePartialLoader.addPart('typeTarif');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'LigneFrais', function($stateParams, LigneFrais) {
                    return LigneFrais.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'ligne-frais',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        });
    }

})();
