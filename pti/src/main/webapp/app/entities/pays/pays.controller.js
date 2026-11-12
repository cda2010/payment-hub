(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PaysController', PaysController);

    PaysController.$inject = ['InitCrud','$scope','$state', 'Pays'];

    function PaysController(InitCrud,$scope,$state, Pays) {

        var vm = this;
        vm.title="Liste des pays";

        vm.columns = [{
            name: 'codePays',
            title: 'code'
        },{
            name: 'libellePays',
            title: 'libelle'
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
                codePays: null,
                libellePays: null,
                id: null
            },
            entity:Pays,
            addTemplateUrl:"pays/pays-dialog.html",
            deleteTemplateUrl:"pays/pays-delete-dialog.html",
            controller:"Pays",
            entityParamsItem:{},
            entitySearchParamsItem:{},
            size:"md",
            scope:$scope
        };
        InitCrud.init(datas);
    }
})();