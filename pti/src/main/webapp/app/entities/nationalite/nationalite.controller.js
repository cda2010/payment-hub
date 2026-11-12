(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('NationaliteController', NationaliteController);

    NationaliteController.$inject = ['InitCrud','$scope','$state', 'Nationalite', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function NationaliteController(InitCrud,$scope,$state, Nationalite, ParseLinks, AlertService, paginationConstants, pagingParams) {

        var vm = this;
        vm.title="Liste des pays";

        vm.columns = [{
            name: 'code',
            title: 'Code'
        },{
            name: 'libelle',
            title: 'Libelle'
        }];

        vm.actions={
            view:false,
            edit:true,
            delete:true,
            add:true
        };

        var datas={
            vm:vm,
            entityDatas:{
                code: null,
                libelle: null,
                id: null
            },
            entity:Nationalite,
            addTemplateUrl:"nationalite/nationalite-dialog.html",
            deleteTemplateUrl:"nationalite/nationalite-delete-dialog.html",
            controller:"Nationalite",
            entityParamsItem:{},
            entitySearchParamsItem:{},
            size:"md",
            scope:$scope
        };
        InitCrud.init(datas);
    }
})();