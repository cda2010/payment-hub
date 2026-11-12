(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('HoraireAuthentificationController', HoraireAuthentificationController);

        HoraireAuthentificationController.$inject = ['InitCrud','$scope','$state', 'HoraireAuthentification'];

        function HoraireAuthentificationController(InitCrud,$scope,$state, HoraireAuthentification) {
    
            var vm = this;
            vm.title="Liste des horaire-authentification";
    
            vm.columns = [{
                name: 'jour',
                title: 'Jour'
            },{
                name: 'heureDebut',
                title: 'HeureDebut'
            },{
                name: 'heureFin',
                title: 'HeureFin'
            }, {
                name: 'activated',
                title: 'Actif ?',
                type:'Boolean'
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
                 
                    id: null
                },
                entity:HoraireAuthentification,
                addTemplateUrl:"horaire-authentification/horaire-authentification-dialog.html",
                deleteTemplateUrl:"horaire-authentification/horaire-authentification-delete-dialog.html",
                controller:"HoraireAuthentification",
                entityParamsItem:{},
                entitySearchParamsItem:{},
                size:"md",
                scope:$scope
            };
            InitCrud.init(datas);
        }
    })();