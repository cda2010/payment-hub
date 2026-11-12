(function () {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('tableau-frais', {
                parent: 'entity',
                url: '/tableau-frais?page&sort&search',
                data: {
                    authorities: ['ROLE_CRE_FRAIS'],
                    pageTitle: 'ptiApp.tableauFrais.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/components/tables/tables.html',
                        controller: 'TableauFraisController',
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
                        $translatePartialLoader.addPart('tableauFrais');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }]
                }
            }).state('frais-ligneFrais', {
                parent: 'tableau-frais',
                url: '/tableau-frais/lignes',
                data: {
                    authorities: ['ROLE_CRE_FRAIS'],
                    pageTitle: 'Frais'
                },
                views: {
                    'frais-ligneFrais': {
                        templateUrl: 'app/components/tables/tablesother.html',
                        controller: 'LigneFraisController',
                        controllerAs: 'vm'
                    }
                },
                params: {
                    tableauFrais: null
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('ligneFrais');
                        $translatePartialLoader.addPart('typeTarif');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }],
                    tableauFrais: function () {
                        return null;
                    }
                    ,$uibModalInstance:function (){
                        return null;
                    }
                }
            });
    }

})();
