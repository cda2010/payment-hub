(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('param-canal', {
            parent: 'entity', 
            url: '/param-canal?page&sort&search',
            data: {  
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.serviceProfil.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/param-canal/param-canal.html',
                    controller: 'ParamCanalController', 
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
                    $translatePartialLoader.addPart('serviceProfil');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('param-canal.new', {
            parent: 'param-canal',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/param-canal/param-canal-dialog.html',
                    controller: 'ParamCanalDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                typeCanal: null,
                                id: null,
                                application:null,
                                canal: null,
                                sousCanal:null,
                                typeFacturier:null,
                                uniteOrganisation:{
                                    id: null
                                },
                                poids: 0

                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('param-canal', null, { reload: 'param-canal' });
                }, function() {
                    $state.go('param-canal');
                });
            }]
        })
        /* .state('param-canal.delete', {
            parent: 'param-canal',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/param-canal/param-canal-delete-dialog.html',
                    controller: 'ParamCanalDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ParamCanal', function(ParamCanal) {
                            return ParamCanal.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('param-canal', null, { reload: 'param-canal' });
                }, function() {
                    $state.go('^');
                });
            }]
        }); */
    }

})();
