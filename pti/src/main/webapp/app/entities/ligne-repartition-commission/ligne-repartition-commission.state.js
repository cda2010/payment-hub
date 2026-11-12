(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('ligne-repartition-commission', {
                parent: 'entity',
                url: '/ligne-repartition-commission?page&sort&search',
                data: {
                    authorities: ['ROLE_CRE_REPART_COMMISSION'],
                    pageTitle: 'ptiApp.ligneRepartitionCommission.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/components/tables/tables.html',
                        controller: 'LigneRepartitionCommissionController',
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
                    pagingParams: ['$stateParams', 'PaginationUtil', function($stateParams, PaginationUtil) {
                        return {
                            page: PaginationUtil.parsePage($stateParams.page),
                            sort: $stateParams.sort,
                            predicate: PaginationUtil.parsePredicate($stateParams.sort),
                            ascending: PaginationUtil.parseAscending($stateParams.sort),
                            search: $stateParams.search
                        };
                    }],
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('ligneRepartitionCommission');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }]
                }
            })
            .state('ligne-repartition-commission-detail', {
                parent: 'ligne-repartition-commission',
                url: '/ligne-repartition-commission/{id}',
                data: {
                    authorities: ['ROLE_CRE_REPART_COMMISSION'],
                    pageTitle: 'ptiApp.ligneRepartitionCommission.detail.title'
                },
                views: {
                    'content@': {
                        templateUrl: 'app/entities/ligne-repartition-commission/ligne-repartition-commission-detail.html',
                        controller: 'LigneRepartitionCommissionDetailController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('ligneRepartitionCommission');
                        return $translate.refresh();
                    }],
                    entity: ['$stateParams', 'LigneRepartitionCommission', function($stateParams, LigneRepartitionCommission) {
                        return LigneRepartitionCommission.get({ id: $stateParams.id }).$promise;
                    }],
                    previousState: ["$state", function($state) {
                        var currentStateData = {
                            name: $state.current.name || 'ligne-repartition-commission',
                            params: $state.params,
                            url: $state.href($state.current.name, $state.params)
                        };
                        return currentStateData;
                    }]
                }
            });
    }

})();