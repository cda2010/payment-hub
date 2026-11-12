(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('group-repartition', {
            parent: 'entity',
            url: '/group-repartition?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.groupRepartition.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/group-repartition/group-repartitions.html',
                    controller: 'GroupRepartitionController',
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
                    $translatePartialLoader.addPart('groupRepartition');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('group-repartition-detail', {
            parent: 'group-repartition',
            url: '/group-repartition/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.groupRepartition.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/group-repartition/group-repartition-detail.html',
                    controller: 'GroupRepartitionDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('groupRepartition');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'GroupRepartition', function($stateParams, GroupRepartition) {
                    return GroupRepartition.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'group-repartition',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('group-repartition-detail.edit', {
            parent: 'group-repartition-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/group-repartition/group-repartition-dialog.html',
                    controller: 'GroupRepartitionDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['GroupRepartition', function(GroupRepartition) {
                            return GroupRepartition.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('group-repartition.new', {
            parent: 'group-repartition',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/group-repartition/group-repartition-dialog.html',
                    controller: 'GroupRepartitionDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                libelleGroup: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('group-repartition', null, { reload: 'group-repartition' });
                }, function() {
                    $state.go('group-repartition');
                });
            }]
        })
        .state('group-repartition.edit', {
            parent: 'group-repartition',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/group-repartition/group-repartition-dialog.html',
                    controller: 'GroupRepartitionDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['GroupRepartition', function(GroupRepartition) {
                            return GroupRepartition.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('group-repartition', null, { reload: 'group-repartition' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('group-repartition.delete', {
            parent: 'group-repartition',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/group-repartition/group-repartition-delete-dialog.html',
                    controller: 'GroupRepartitionDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['GroupRepartition', function(GroupRepartition) {
                            return GroupRepartition.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('group-repartition', null, { reload: 'group-repartition' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
