(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EntityAuditEventController', EntityAuditEventController);

    EntityAuditEventController.$inject = ['$state', 'TableService', '$scope','EntityAuditEvent', 'EntityTypeEvent','ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function EntityAuditEventController($state, TableService, $scope, EntityAuditEvent, EntityTypeEvent,ParseLinks, AlertService, paginationConstants, pagingParams) {

        var vm = this;

        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.filtrer=true;
        vm.canShowResultCount=false;
        // var action=null;
        // var adresse=null;
       // loadAll();
        vm.itemSearch={
            "action":null,
            "adresse":null,
            "modifiedBy":null
        };
        EntityTypeEvent.query(function (result) {
            console.log(result);
            vm.totalItems=result.length+1;
        });
        // action=vm.itemSearch.action;
        // adresse=vm.itemSearch.adresse;
        function loadAll () {
            console.log(action);
            EntityAuditEvent.queryAll({
                page: pagingParams.page - 1,
                size: vm.itemsPerPage,
                sort: sort(),
                action:action,
                adresse:adresse
            }, onSuccess, onError);
            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'id') {
                    result.push('id');
                }
                return result;
            }
            function onSuccess(data, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.entityAuditEvents = data;
                vm.page = pagingParams.page;
                console.log(data,vm.links)
            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }
        function init() {

            var addItemData = {};

            /**
             *Modification d'un élément
             */
            var editItemData = {};

            /**
             * Suppresion d'un élément
             */
            var deleteItemData = {};

            var detailItemData = {};

            var dialogData = {
                addData: addItemData,
                editData: editItemData,
                deleteData: deleteItemData,
                detailData: detailItemData
            };
            var entitySearchParams = function () {
                return {};
            };


            var entityParams = function () {
                return {
                    "action":vm.itemSearch.action,
                    "adresse": vm.itemSearch.adresse,
                    "modifiedBy": vm.itemSearch.modifiedBy
                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                entity: EntityTypeEvent,
                entitySearch:EntityTypeEvent,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
            vm.canShowResultCount=true;
        }

        vm.filtre = function () {
            console.log("filtre des informations en cours");
            init();
           //loadAll();
        };
        vm.annulerFiltre= function () {
            vm.itemSearch={
                "action":null,
                "adresse":null,
                "modifiedBy":null
            };
        };
        function loadPage(page) {
            vm.page = page;
            vm.transition();
        }

        function transition() {
            $state.transitionTo($state.$current, {
                page: vm.page,
                sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
                search: vm.currentSearch,
                action:action,
                adresse:adresse
            });
        }
    }
})();
