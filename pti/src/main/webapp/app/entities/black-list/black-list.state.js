(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('black-list', {
            parent: 'entity',
            url: '/black-list?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.blackList.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/black-list/black-lists.html',
                    controller: 'BlackListController',
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
                    $translatePartialLoader.addPart('blackList');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('black-list-detail', {
            parent: 'black-list',
            url: '/black-list/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.blackList.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/black-list/black-list-detail.html',
                    controller: 'BlackListDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('blackList');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'BlackList', function($stateParams, BlackList) {
                    return BlackList.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'black-list',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('black-list-detail.edit', {
            parent: 'black-list-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/black-list/black-list-dialog.html',
                    controller: 'BlackListDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['BlackList', function(BlackList) {
                            return BlackList.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('black-list.new', {
            parent: 'black-list',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/black-list/black-list-dialog.html',
                    controller: 'BlackListDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                id: null,
                                clientId:null,
                                actionBlackList: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('black-list', null, { reload: 'black-list' });
                }, function() {
                    $state.go('black-list');
                });
            }]
        })
        .state('black-list.edit', {
            parent: 'black-list',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/black-list/black-list-dialog.html',
                    controller: 'BlackListDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['BlackList', function(BlackList) {
                            return BlackList.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('black-list', null, { reload: 'black-list' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('black-list.delete', {
            parent: 'black-list',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/black-list/black-list-delete-dialog.html',
                    controller: 'BlackListDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['BlackList', function(BlackList) {
                            return BlackList.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('black-list', null, { reload: 'black-list' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
