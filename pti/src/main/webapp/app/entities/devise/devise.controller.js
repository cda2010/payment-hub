(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DeviseController', DeviseController);

    DeviseController.$inject = ['$scope','InitCrud','$state', 'Devise', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function DeviseController($scope,InitCrud,$state, Devise, ParseLinks, AlertService, paginationConstants, pagingParams) {
        var vm = this;
        vm.title="Liste des devises";

        vm.columns = [{
            name: 'code',
            title: 'Code'
        }, {
            name: 'libelle',
            title: 'Libelle'
        },{
            name: 'symbole',
            title: 'Symbole'
        }];

        vm.actions={
            view:false,
            edit:true,
            delete:true,
        };

        var datas={
            vm:vm,
            entityDatas:{
                code: null,
                libelle: null,
                symbole: null,
                id: null
            },
            entity:Devise,
            addTemplateUrl:"devise/devise-dialog.html",
            deleteTemplateUrl:"devise/devise-delete-dialog.html",
            controller:"Devise",
            entityParamsItem:{},
            entitySearchParamsItem:{},
            size:"md",
            scope:$scope
        };
        InitCrud.init(datas);
    }
})();