(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('tableau-repartition-commission', {
                parent: 'entity',
                url: '/tableau-repartition-commission?page&sort&search',
                data: {
                    authorities: ['ROLE_CRE_REPART_COMMISSION'],
                    pageTitle: 'ptiApp.tableauRepartitionCommission.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/components/tables/tables.html',
                        controller: 'TableauRepartitionCommissionController',
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
                        $translatePartialLoader.addPart('tableauRepartitionCommission');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }]
                }
            }).state('tableau-ligne-repartition-commission', {
                parent: 'tableau-repartition-commission',
                url: '/lignes',
                data: {
                    authorities: ['ROLE_CRE_REPART_COMMISSION'],
                    pageTitle: 'Lignes'
                },
                views: {
                    'tableau-ligne-repartition-commission': {
                        templateUrl: 'app/components/tables/tables.html',
                        controller: 'LigneRepartitionCommissionController',
                        controllerAs: 'vm'
                    }
                },
                params: {
                    tableauRepartition: null
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('ligneRepartitionCommission');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }],
                    tableauRepartition: function () {
                        return null;
                    }
                    ,$uibModalInstance:function (){
                        return null;
                    }
                }
            });
    }

})();
